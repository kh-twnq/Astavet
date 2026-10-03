#!/usr/bin/env python3
"""Read effective Codex config, skill discovery and hook trust; no model turns.
Requires the Codex CLI and access to its user cache. Outputs only selected
non-secret metadata; never grants hook trust or modifies plugin settings.
"""
import argparse
import json
import subprocess
import selectors
import os
import time
from pathlib import Path
parser = argparse.ArgumentParser(description=__doc__)
parser.add_argument('--project', default=os.getcwd(), help='Target project directory (defaults to current directory)')
parser.add_argument('--mcp', action='store_true', help='Also start configured MCP servers to inspect startup status')
args = parser.parse_args()
root=str(Path(args.project).expanduser().resolve())
if not Path(root).is_dir():
 parser.error('--project must be an existing directory')
p=subprocess.Popen(['codex','app-server','--strict-config','--stdio'],cwd=root,stdin=subprocess.PIPE,stdout=subprocess.PIPE,stderr=subprocess.DEVNULL)
sel=selectors.DefaultSelector();sel.register(p.stdout,selectors.EVENT_READ);buffer=b''
def call(id,method,params):
 global buffer
 p.stdin.write((json.dumps({'id':id,'method':method,'params':params})+'\n').encode());p.stdin.flush()
 end=time.monotonic()+25
 while time.monotonic()<end:
  while b'\n' in buffer:
   line,buffer=buffer.split(b'\n',1)
   try:d=json.loads(line)
   except:continue
   if d.get('id')==id:return d
  if p.poll() is not None:raise RuntimeError('app-server exited')
  if sel.select(1):buffer+=os.read(p.stdout.fileno(),65536)
 raise RuntimeError('read timeout: '+method)
try:
 call(1,'initialize',{'clientInfo':{'name':'codex_ecc_audit','version':'1.0'},'capabilities':{'experimentalApi':True}})
 p.stdin.write(b'{"method":"initialized"}\n');p.stdin.flush()
 report={'project': root}
 requests = [(2,'config/read',{'cwd':root,'includeLayers':True}),(3,'skills/list',{'cwds':[root],'forceReload':True}),(4,'hooks/list',{'cwds':[root]})]
 if args.mcp:
  requests.append((5, 'mcpServerStatus/list', {'limit': 100, 'detail': 'full'}))
 for id,method,params in requests:
  response=call(id,method,params)
  if 'error' in response:report[method]={'error':response['error'].get('code')};continue
  r=response.get('result',{})
  if method=='config/read':
   c=r.get('config',{})
   report[method]={'agents':c.get('agents'),'features':{k:v for k,v in c.get('features',{}).items() if k in ['multi_agent','multi_agent_v2','hooks','plugins']},'approval_policy':c.get('approval_policy'),'sandbox_mode':c.get('sandbox_mode'),'layers':[{'name':layer.get('name'),'disabledReason':layer.get('disabledReason')} for layer in r.get('layers',[])]}
  elif method=='skills/list':
   report[method]={'entries':[]}
   for entry in r.get('data',[]):
    skills=entry.get('skills',[])
    chosen=[{'name':s.get('name'),'path':s.get('path'),'enabled':s.get('enabled'),'scope':s.get('scope')} for s in skills if s.get('path','').startswith(root+'/.agents/') or s.get('pluginId') == 'ecc@ecc' or s.get('name','').startswith('ecc:')]
    report[method]['entries'].append({'skills':chosen,'errors':entry.get('errors',[])})
  elif method == 'hooks/list':
   report[method]={'shape':list(r),'entries':[]}
   for entry in r.get('data',[]):
    report[method]['entries'].append({'errors':entry.get('errors',[]),'warnings':entry.get('warnings',[]),'hooks':[ {k:h.get(k) for k in ['eventName','enabled','source','pluginId','trustStatus','sourcePath','statusMessage']} for h in entry.get('hooks',[])]})
  else:
   report[method] = {'servers': [{k: server.get(k) for k in ['name', 'authStatus', 'runtimeStatus', 'toolsError', 'serverInfo']} | {'toolCount': len(server.get('tools', {}))} for server in r.get('data', []) if server.get('name') in ['chrome-devtools', 'astavet-chrome-devtools', 'ecc-chrome-devtools']]}
 print(json.dumps(report,indent=2))
finally:
 p.terminate()
 try:p.wait(timeout=5)
 except subprocess.TimeoutExpired:p.kill()

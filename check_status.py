import urllib.request, json, time, sys

url = 'https://api.github.com/repos/dharani2006lakshmi-sys/LYRA/actions/runs?per_page=5'
print("Watching v6.0.2 build...")
while True:
    try:
        req = urllib.request.Request(url, headers={'User-Agent': 'Mozilla/5.0'})
        with urllib.request.urlopen(req) as response:
            data = json.loads(response.read().decode())
            runs = data.get('workflow_runs', [])
            
            # Find the v6.0.3 build
            v6_run = next((r for r in runs if r.get('head_branch') == 'v6.0.3'), None)
            
            if not v6_run:
                print("Could not find v6.0.3 build.")
                sys.exit(1)
                
            status = v6_run.get('status')
            conclusion = v6_run.get('conclusion')
            
            if status == 'completed':
                print(f"Build Completed! Conclusion: {conclusion}")
                sys.exit(0)
            else:
                print(f"Status: {status} - waiting 30 seconds...")
                
    except Exception as e:
        print(f"Error: {e}")
        
    time.sleep(30)

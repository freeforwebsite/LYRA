import urllib.request, json

url = 'https://api.github.com/repos/dharani2006lakshmi-sys/LYRA/actions/runs?per_page=5'
req = urllib.request.Request(url, headers={'User-Agent': 'Mozilla/5.0'})
try:
    with urllib.request.urlopen(req) as response:
        data = json.loads(response.read().decode())
        runs = data.get('workflow_runs', [])
        v6_run = next((r for r in runs if r.get('head_branch') == 'v6.0.2'), None)
        if v6_run:
            jobs_url = v6_run.get('jobs_url')
            jobs_req = urllib.request.Request(jobs_url, headers={'User-Agent': 'Mozilla/5.0'})
            with urllib.request.urlopen(jobs_req) as j_resp:
                j_data = json.loads(j_resp.read().decode())
                for job in j_data.get('jobs', []):
                    if job.get('conclusion') == 'failure':
                        print(f"Failed Job: {job.get('name')}")
                        for step in job.get('steps', []):
                            if step.get('conclusion') == 'failure':
                                print(f"Failed Step: {step.get('name')}")
except Exception as e:
    print(e)

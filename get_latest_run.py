import urllib.request, json, ssl

url = 'https://api.github.com/repos/dharani2006lakshmi-sys/LYRA/actions/runs?per_page=1'
req = urllib.request.Request(url, headers={'User-Agent': 'Mozilla/5.0'})
try:
    ctx = ssl.create_default_context()
    ctx.check_hostname = False
    ctx.verify_mode = ssl.CERT_NONE
    with urllib.request.urlopen(req, context=ctx) as response:
        data = json.loads(response.read().decode('utf-8'))
        runs = data.get('workflow_runs', [])
        if runs:
            r = runs[0]
            print(f"Run ID: {r.get('id')} Branch: {r.get('head_branch')} Status: {r.get('status')} Conclusion: {r.get('conclusion')}")
        else:
            print("No runs found.")
except Exception as e:
    print(f"Error: {e}")

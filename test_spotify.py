import urllib.request, re, json

req = urllib.request.Request('https://open.spotify.com', headers={'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64)'})
try:
    with urllib.request.urlopen(req) as response:
        html = response.read().decode('utf-8')
        match = re.search(r'<script id="session" data-testid="session" type="application/json">(.*?)</script>', html)
        if match:
            session_data = json.loads(match.group(1))
            token = session_data.get('accessToken')
            print('Token:', token[:10] if token else None, '...')
        else:
            print('No session data found')
except Exception as e:
    print(e)

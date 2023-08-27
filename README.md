# GitHub manager application

### About

Application provides simple API (one endpoint) to fetch information about GitHub user repositories. Only requests
with header `Accept: application/json` would be accepted. Forked repositories will be skipped.

### API
- **/api/users/{username}** - get information about user's repositories

### Example responses
 - **200 OK**
```bash
curl -i -H 'Accept: application/json' http://localhost:8080/api/users/kklimas
```
```bash
HTTP/1.1 200 OK
Content-Type: application/json
Content-Length: 2076
[
    {
        "repositoryName": "flight-app",
        "ownerLogin": "kklimas",
        "branches": [
            {
                "branchName": "FA-001",
                "lastCommitSha": "1409da33fafd21da9e15d7bd909b94b9da7a94ee"
            },
            {
                "branchName": "Krzysiek",
                "lastCommitSha": "ce24001c0a21994746f6c3a383ec04bd5ab207e7"
            },
            {
                "branchName": "main",
                "lastCommitSha": "da0a2449a4a5a372ca601dd0b48c025b27330b51"
            }
        ]
    },
    {
        "repositoryName": "introduction-to-web-apps",
        "ownerLogin": "kklimas",
        "branches": [
            {
                "branchName": "main",
                "lastCommitSha": "ac004bd19b8736bb71014ea05d096e7cf592c4f7"
            }
        ]
    }
]
```
- **403 Forbidden** (after few request when using free api access)
```bash
curl -i -H 'Accept: application/json' http://localhost:8080/api/users/kklimas
```
```bash
HTTP/1.1 403 Forbidden
Content-Type: application/json
Content-Length: 187

{
  "code":403,
  "message":"API rate limit exceeded for {IP} (But here's the good news: Authenticated requests get a higher rate limit. Check out the documentation for more details.)"
}    
```
- **404 Not found**
```bash
curl -i -H 'Accept: application/json' http://localhost:8080/api/users/kklimasss
```
```bash
HTTP/1.1 404 Not Found
Content-Type: application/json
Content-Length: 34

{
  "code":404,
  "message":"Not Found"
}   
```
- **406 Not acceptable**
```bash
curl -i -H 'Accept: application/xml' http://localhost:8080/api/users/kklimas
```
```bash
HTTP/1.1 406 Not Acceptable
Content-Type: application/json
Content-Length: 87

{
  "code":406,
  "message":"Only request with media type application/json can be accepted."
}    
```
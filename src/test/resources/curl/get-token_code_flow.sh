#initia URL
#https://oauth.magticom.ge/auth/oauth/authorize?client_id=SimpleAuthClient&redirect_uri=http://localhost:8082/pitalo/login&response_type=code&state=18Tj2h
#https://oauth.magticom.ge/auth/oauth/authorize?client_id=OTTAppId&redirect_uri=magticomott://auth&response_type=code&state=18Tj2h

#!/bin/bash
result=$(curl -s --user SimpleAuthClient:secret -X POST "https://oauth.magticom.ge/auth/oauth/token?grant_type=authorization_code&redirect_uri=http://localhost:8082/pitalo/login&client_id=SimpleAuthClient&code=QYVDQI"   )
echo "$result"
tok=$(echo "$result" | jq -r '.access_token')
echo "tok=$tok"
curl -H "accept: */*" -H "Content-Type: application/json" -H "Authorization: Bearer $tok" -X GET "http://localhost:8080/simple-auth/simple/hello"   -w  " ResponseTime=%{time_starttransfer} statusCode=%{http_code}\n"




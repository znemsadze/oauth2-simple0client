#!/bin/bash
id=$1
result=$(curl -s --user SimpleAuthClient:secret -X POST "https://oauth.magticom.ge/auth/oauth/token?grant_type=ldap_auth&username=zviad.nemsadze&password=xxxxx!&client_id=SimpleAuthClient" )

token=$(echo "$result" | jq -r '.access_token')
echo $token
curl -H "accept: */*" -H "Content-Type: application/json" -H "Authorization: Bearer $token" -X GET "http://localhost:8080/simple-auth/simple/hello"   -w  " ResponseTime=%{time_starttransfer} statusCode=%{http_code}\n"


# https://openai.apifox.cn/api-67883981
curl https://apis.itedus.cn/v1/chat/completions \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer sk-MHBrFWwiCwrYR4FU1c678479833c421f8dAd5418C03f3fE6" \
  -d '{
    "model": "gpt-4o",
    "stream": true,
    "messages": [
      {
        "role": "system",
        "content": "You are a helpful assistant."
      },
      {
        "role": "user",
        "content": "1+1"
      }
    ]
  }'

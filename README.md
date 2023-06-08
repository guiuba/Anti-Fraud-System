# Anti-Fraud-System
Part of my learning journey at JetBrains Academy, stage 6/6.
This project demonstrates (in a simplified form) the principles of anti-fraud systems in the financial sector.  
It has a set of REST endpoints responsible for interacting with users, and an internal transaction validation 
logic based on a set of heuristic rules.

Examples
Think of the examples as a chain of consecutive and related events.
Example 1: a POST request for /api/antifraud/transaction

Request body:

{
  "amount": 210,
  "ip": "192.168.1.1",
  "number": "4000008449433403",
  "region": "EAP",
  "date": "2022-01-22T16:04:00"
}
Response: 200 OK

Response body:

{
   "result": "MANUAL_PROCESSING",
   "info": "amount"
}
Example 2: a GET request for /api/antifraud/history/4000008449433403

Response: 200 OK

Response body:

[
  {
  "transactionId": 1,
  "amount": 210,
  "ip": "192.168.1.1",
  "number": "4000008449433403",
  "region": "EAP",
  "date": "2022-01-22T16:04:00",
  "result": "MANUAL_PROCESSING",
  "feedback": ""
  }
]
Example 3: a PUT request for /api/antifraud/transaction

Request body:

{
   "transactionId": 1,
   "feedback": "ALLOWED"
}
Response: 200 OK

Response body:

{
  "transactionId": 1,
  "amount": 210,
  "ip": "192.168.1.1",
  "number": "4000008449433403",
  "region": "EAP",
  "date": "2022-01-22T16:04:00",
  "result": "MANUAL_PROCESSING",
  "feedback": "ALLOWED"
}
Example 4: a POST request for /api/antifraud/transaction

Request body:

{
  "amount": 100,
  "ip": "192.168.1.1",
  "number": "4000008449433403",
  "region": "EAP",
  "date": "2022-01-22T16:05:00"
}
Response: 200 OK

Response body:

{
   "result": "ALLOWED",
   "info": "none"
}
Example 5: a GET request for /api/antifraud/history/4000008449433403

Response: 200 OK

Response body:

[
  {
  "transactionId": 1,
  "amount": 210,
  "ip": "192.168.1.1",
  "number": "4000008449433403",
  "region": "EAP",
  "date": "2022-01-22T16:04:00",
  "result": "MANUAL_PROCESSING",
  "feedback": "ALLOWED"
  },
  {
  "transactionId": 2,
  "amount": 210,
  "ip": "192.168.1.1",
  "number": "4000008449433403",
  "region": "EAP",
  "date": "2022-01-22T16:05:00",
  "result": "ALLOWED",
  "feedback": ""
  }
]
Example 6: a PUT request for /api/antifraud/transaction

Request body:

{
   "transactionId": 2,
   "feedback": "ALLOWED"
}
Response: 422 Unprocessable Entity

Example 7: a PUT request for /api/antifraud/transaction

Request body:

{
   "transactionId": 2,
   "feedback": "MAY BE OK"
}
Response: 400 Bad Request

Example 8: a PUT request for /api/antifraud/transaction

Request body:

{
   "transactionId": 1,
   "feedback": "PROHIBITED"
}
Response: 409 Conflict

Example 9: a GET request for /api/antifraud/history/4000008449433402; the card number fails the Luhn algorithm

Response: 400 Bad Request

Example 10: a GET request for /api/antifraud/history/4000009455296122

Response: 404 Not Found

GET

/api/v1/health

Response

{
  status
  application
  version
}

--------------------------------

POST

/api/v1/transactions

Request

{
 customerId
 merchantId
 amount
 currency
}

Response

201 Created

{
 message
}
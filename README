## Car Advert web-service

This is a web-service that allows users to place new car adverts and modifies the existing ones.
The service is written in Scala v.2.11.7 by means of Play! framework v.2.5.

Car advert service speaks in JSON format. It also requires that PUT and POST requests have to be made within JSON,
so please make sure you pass a correct content type
> `Content-Type`: application/json

Otherwise you will receive _400: Bad Request_ error with message:
```
{
    "message": "Expected application/json request."
}
```

### Up and Run
Simply navigate to the root folder and run a command
> activator run
That will boot a server. By default server runs on port: 9000, you can freely change this setting
as well as other in _application.conf_ file, which is located at <root folder>/conf/application.conf.
For more details on settings, please visit the [page](https://www.playframework.com/documentation/2.5.x/Configuration)

### Car Advert
Car advert has several fields that need to be set and couple of auxiliary ones:
* **id** (_required_): **int** - indicates unique id of the advert;
* **title** (_required_): **string**, e.g. _"Audi A4 Avant"_;
* **fuel** (_required_): `gasoline` or `diesel`;
* **price** (_required_): **integer**;
* **new** (_required_): **boolean**, indicates if car is new or used;
* **mileage** (_only for used cars_): **integer**;
* **first_registration** (_only for used cars_): **date** without time.


### Routing

| Verb    | URI             | Description                              |
|---------|-----------------|------------------------------------------|
| GET     | /api/car-ads    | returns a list of all car adverts.       |
| GET     | /api/car-ad/:id | returns data for single car advert by id |
| PUT     | /api/car-ad     | adds car advert                          |
| POST    | /api/car-ad/:id | modifies car advert by id                |
| DELETE  | /api/car-ad/:id | deletes car advert by id                 |
| OPTIONS | /api/car-ads    | lists available methods                  |

You can choose sorting order in case of _/api/car-ads_. To do this, you need to pass _orderBy_ parameter
with the filed name specifying sorting priority.
The name are next :
* **id** - default sorting
* **title** - all adverts will be sorted in alphabetical order
* **fuel** -  simply groups all adverts by fuel type and lists in id ascending order
* **price** - use this ordering to see cars from lower to high price.
* **new** -  simply groups all adverts to represent new cars in the top of list followed by old ones.
* **mileage** - orders in ascending order by this field.
* **first_registration** - you will get a list from oldest ot newest one. The new car come in the end of list.

### Examples
> http://localhost:9000/api/car-ads?orderBy=title
```
{
  "adverts": [
    {
      "id": 18,
      "title": "Audi A4",
      "fuel": "diesel",
      "price": "10000",
      "new": false,
      "mileage": "43000",
      "first_registration": "2008-09-14"
    },
    {
      "id": 19,
      "title": "Kia Ceed",
      "fuel": "gasoline",
      "price": "8000",
      "new": false,
      "mileage": "27000",
      "first_registration": "2013-05-28"
    },
    {
      "id": 20,
      "title": "Skoda Octavia",
      "fuel": "diesel",
      "price": "25000",
      "new": true,
      "mileage": "",
      "first_registration": ""
    }
  ]
}
```

> http://localhost:9000/api/car-ads?orderBy=price
```
{
  "adverts": [
    {
      "id": 19,
      "title": "Kia Ceed",
      "fuel": "gasoline",
      "price": "8000",
      "new": false,
      "mileage": "27000",
      "first_registration": "2013-05-28"
    },
    {
      "id": 18,
      "title": "Audi A4",
      "fuel": "diesel",
      "price": "10000",
      "new": false,
      "mileage": "43000",
      "first_registration": "2008-09-14"
    },
    {
      "id": 20,
      "title": "Skoda Octavia",
      "fuel": "diesel",
      "price": "25000",
      "new": true,
      "mileage": "",
      "first_registration": ""
    }
  ]
}
```

> http://localhost:9000/api/car-ads?orderBy=fuel
```
{
  "adverts": [
    {
      "id": 18,
      "title": "Audi A4",
      "fuel": "diesel",
      "price": "10000",
      "new": false,
      "mileage": "43000",
      "first_registration": "2008-09-14"
    },
    {
      "id": 20,
      "title": "Skoda Octavia",
      "fuel": "diesel",
      "price": "25000",
      "new": true,
      "mileage": "",
      "first_registration": ""
    },
    {
      "id": 19,
      "title": "Kia Ceed",
      "fuel": "gasoline",
      "price": "8000",
      "new": false,
      "mileage": "27000",
      "first_registration": "2013-05-28"
    }
  ]
}
```



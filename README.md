# Trash Mail Detector

The Trash Mail Detector API is a RESTful API that allows you to detect throwaway email addresses.

Currently, there are about 60,000 domains in the database and it is updated automatically almost every day.

This makes it easy to find email addresses that are unlikely to be legitimate, 
such as those belonging to a spammer or phisher.

--

5-minute emails or also known as trash emails, 
are usually used to protect the data protection and privacy of users. 
However, there are applications that are designed to contact users at any time, 
which is not the case with 5-minute emails or other trash emails. 
In order to prevent users from registering with such emails, 
it is necessary to take preventive measures.

## How to use?

---

### Domain:

**GET:** https://trashmaildetector.yourDomain.tld/check?validate=trashmail.com
```bash
{
  "processing ": "0ms",
  "provided": "domain",
  "domain": "trashmail.com",
  "status": "suspicious"
}
```
---
### Email:

**GET:** https://trashmaildetector.yourDomain.tld/check?validate=joe@trashmail.com
```bash
{
  "processing ": "0ms",
  "provided": "email",
  "domain": "trashmail.com",
  "status": "suspicious"
}
```

--- 

The status if the domain/email is suspicious can be seen via the key 'status'.

**Suspicious**: ``"status": "suspicious"``

**Unsuspicious**: ``"status": "unsuspicious"``
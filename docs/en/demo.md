> This Demo demonstrates the use of Easy-Es in the Springboot project. It is recommended to download it first and run it directly on your local.
> Demo download link: [Github](https://github.com/xpc1024/easy-es-springboot-demo-en)

# Demo introduction

---

## 1. Project structure

---

![](https://cdn.nlark.com/yuque/0/2021/png/21559896/1638952452520-10d0b15d-4a46-4d0e-9c6d-b3a4f859e587.png#crop=0&crop=0&crop=1&crop=1&from=url&id=XbyNe&margin=%5Bobject%20Object%5D&originHeight=299&originWidth=400&originalType=binary&ratio=1&rotation=0&showTitle=false&status=done&style=none&title=)

## 2.Configuration

---

```yaml
easy-es:
  eanble: true # The default value is true, If the value of enable is false, it is considered that Easy-es is not enabled
  address: 127.0.0.0:9200 # Your elasticsearch address,must contains port, If it is a cluster, please separate with',' just like this: 127.0.0.0:9200,127.0.0.1:9200
  username: elastic # Es username, Not necessary, If it is not set in your elasticsearch, delete this line
  password: WG7WVmuNMtM4GwNYkyWH # Es password, Not necessary, If it is not set, delete this line

```
## 3.Run

---

Use your IDE to start the project:<br />![](https://cdn.nlark.com/yuque/0/2021/png/21559896/1638952870873-8d460b56-327f-4fa7-99cd-a65e64ce0716.png#crop=0&crop=0&crop=1&crop=1&from=url&id=qLoIq&margin=%5Bobject%20Object%5D&originHeight=42&originWidth=335&originalType=binary&ratio=1&rotation=0&showTitle=false&status=done&style=none&title=)

## 4.Use

---

Use your browser or postman to request the following addresses in turn:

- [http://localhost:8080/index](http://localhost:8080/index) (create index, must be requested first and only once)
- [http://localhost:8080/insert](http://localhost:8080/insert) (create data)
- [http://localhost:8080/search](http://localhost:8080/search) (search data)
> Then you will get the result what you searched in your browser or postman.

## 5.Summary

---

At this point, you have initially experienced the basic functions of Easy-Es. If you feel that the experience is good, and you want to further experience more powerful functions, you can check the reference document.

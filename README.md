# ST-DEF-GEN  

ST-DEF-GEN generates Swagger 2.0 definition in json format for **Semantic Turkey** created by the ART Research Group at the University of Rome, Tor Vergata.

The acronym stands for Semantic Turkey DEFinition GENerator and it is sometimes abbreviated with SDG. 


# Features

* The definition is created by [Swagger-Maven-Plugin by kongchen](https://github.com/kongchen/swagger-maven-plugin)
* Supports [Semantic Turkey](http://semanticturkey.uniroma2.it/)
* Supports [Swagger Specification 2.0](https://github.com/swagger-api/swagger-spec/blob/master/versions/2.0.md)
* Supports Windows & Linux

# Versions
- 0.0.1 released in Github repo

# FAQ

## 1. Building from source

To build from source and get the executable jar, you should:
```
mvn package
```
This will result in a archive "sdg.jar". 

## 2. Run the executable archive

Simply:
```
java -jar sdg.jar 
```
You can specify the attributes in the same instruction or at run-time

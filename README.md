# sever-extension-java
Server extensions allow developers to change and extend the capabilities of ArcGIS Server map and 
image services. This project provides a template intended to bootstrap the development of both types of Java-based 
server extensions, Server Object Extensions (SOEs) and Server Object Interceptors (SOEs). It also includes two REST-based
examples, a simple row-level security filter SOI and an SOE for server-side clustering. In general, this project limits itself on 
REST server extensions.

## Features
* Maven project template - A template project to use as a starting point for developing server extensions
* Boilerplate code - Commonly used glue-code to connect custom business logic with the GIS Server framework
* Simple row-level security filter - An example SOI for implementing row-level access control for query operations 
* Server-side clustering - An example SOE for grouping point data into clusters server-side and returns the generated clusters as features

## Instructions
1. Fork and then clone the repo. 
2. Run and try the examples.

## Requirements
* ArcGIS Enterprise (ArcGIS Server) 10.5
* ArcGIS Desktop 10.5
* ArcObjects SDK for Java 10.5
* Java 8 Development Environment

## Resources
* [About extending services](http://server.arcgis.com/en/server/latest/publish-services/windows/about-extending-services.htm)
* [ArcObjects Help for Java](http://desktop.arcgis.com/en/arcobjects/latest/java/)

## Issues
Find a bug or want to request a new feature?  Please let us know by submitting an issue.

## Contributing
Esri welcomes contributions from anyone and everyone. Please see our [guidelines for contributing](https://github.com/esri/contributing).

## Licensing
Copyright 2017 Esri

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

A copy of the license is available in the repository's [license.txt](/license.txt) file.

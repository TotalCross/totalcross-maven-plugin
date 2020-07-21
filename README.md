# TotalCross Maven Plugin
This is the totalcross maven plugin. It helps building TotalCross applications without download or instaall anything else. You just need to have totalcross-sdk java api set in your dependencies and this plugin takes care of downloading the right TotalCross SDK.

## Tasks
| Task                   | Description                                                                                  |
|------------------------|----------------------------------------------------------------------------------------------|
| totalcross:retrolambda | Uses retrolambda to make project new byte code versions, i.e., 1.8 compatible with java 1.6. |
| totalcross:package     | Executes the package process required to make totalcross applications.                       |
|                        |                                                                                              |
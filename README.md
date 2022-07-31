# View Spot Finder Demo App

Execution as .jar:

```
java -jar ViewSpotFinder.jar <path to mesh file> <number of view spots>
```

Local execution with the serverless framework:

```
serverless invoke local --function findViewSpots -p <path to mesh file>
```
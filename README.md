# SmartElevator

Use case description:
Demo an elevator system such that there are n number of banks, each elevator bank has m elevators. The building has f number of floors. On each floor, there is only one call button that specify the floor a user wants to go to.

Assumption:
Assumption:
1. As soon as Elevator opened the door on the starting floor, this elevator is removed from available active Elevators in application. However, it will only be treated back as idle only when it finished stopping on all assigned floor
2. Cart in code means elevator. Use it for typing convenience
3. System will not consider optimization on picking the idle cart that is nearest to the request's starting floor

Basic algorithm:
  * All request will be clustered into 2 category: upward and downward. Each request will be put into one of the cluster based on delta or current floor and destination floor.
  * System is using starting flr to check if cart can fill in more people. Taking upward scenario, if earlier request is upward 3->9, then another request comes as 6->12, then system combines both requests/persons together and will stop @ [3,6,9,12]. However, if second request come as 1->?, because 1 is lower than 3, then initial cart starting floor under the assumption of not being reset, a new cart will be assigned


How to run the application:
A quick demo for convenience is put in object Demo to run.

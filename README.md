# Redis setup
[Follow the "Getting started" instructions to set up Redis appropriate for your operating system](https://redis.io/learn/howtos/quick-start)
There are instructions for Docker, Linux, Windows, and MacOS.

# Running the program
Within the persistance-framework, run `mvn clean package` to package the code into a jar. Then run `java -jar target/annotations-1.0-SNAPSHOT.jar` to run the program. The program will parse the input.json, store the data into Redis, and prompt input for a post ID to load the corresponding post and their associated replies.


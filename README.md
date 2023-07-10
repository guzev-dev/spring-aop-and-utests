# About project:

This is just a small publishing app project to practice aspect-oriented programming, unit testing and
integration with MongoDB database.

Tech stack:
* Java 17
* Spring (Boot, Data MongoDB, AOP)
* Testing: JUnit5, Mockito
* Java Mail Sender

---

### Deployment:

Repository contains `docker-compose.yml` file for quick application deployment.
All you need is:
1. Have installed **Docker Engine** and **Docker Compose** 
(included in **[Docker Desktop](https://docs.docker.com/desktop/)**).
2. Create `.env` file in the same directory as `docker-compose.yml` and 
define environmental variables for Java Mail Sender:
   * *`PETPROJ_MAIL_HOST`* - mailing server host; 
   * *`PETPROJ_MAIL_PORT`* - mailing server port;
   * *`PETPROJ_MAIL_USERNAME`* - email address;
   * *`PETPROJ_MAIL_PASSWORD`* - email password.
3. Using the OS command prompt, navigate to the directory with `docker-compose.yml` and `.env` files and 
start containers with running **Docker** using the `docker-compose up` command.
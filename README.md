# Profile Reporting Service
The Profile Reporting Service is part of the Help Scout Java coding exercise

# Running the project
### Requirements
- JDK SE (>= 8)
- Maven (>= 3) - 3.6.1 was used to build this project

### Directions
1. In the same directory of the pom.xml file:

mvn clean install 

2. A successful build will produce a jar file in target/mediandb-1.0-SNAPSHOT.jar
3. java -jar target/mediandb-1.0-SNAPSHOT.jar
4. Send some REST API requests, either with the browser or with the CURL program:

- curl "http://localhost:8080/birthday/add?birthday=1999-01-05"
> {"birthdayAdded":"1999-01-05","timeAdded":"2019-08-20 02:15:22 EDT"}

- curl "http://localhost:8080/birthday/medianage?start=1900-01-01&end=2009-01-04"
> {"medianAge":20,"fulfillmentTime":"2019-08-20 02:17:27 EDT"}

### Limitations:
This program only supports a date range of 1850-01-01 to 2049-12-31.

### Assumptions I Made:
I assumed that birthdays were the only information needed to be stored about the user. I decided to deal only with storing birthdays, to the exclusion of all other user data, in order to focus on the task at hand, and to keep things as simple as possible.

### Design
The heart of this program is in the ArrayDateHistogram class. I use a 1-dimensional int array to represent 3 histograms: 1 pertaining to the number of birthdays recorded in each year (e.g. 1999), 1 pertaining to the number of birthdays recorded in each month (e.g. 1999-02), and 1 pertaining to the number of birthdays in each date (e.g. 1999-02-03).

Going from start date to end date, the logic would traverse years, months, and dates (as necessary), counting how many birthdates it encountered. It would then traverse until it found half that number (or middle number, depending on whether the number of birthdays between start and end were even or odd)

The advantage of this approach is that copying and persisting the entire histogram is very fast. Another advantage is that memory usage does not grow as more birthdays are added.
The disadvantage of this approach is that traversing many years/months/dates is necessary, even if the number of birthdays added is very small.
In addition, there is also the limitation of a 200 year span (completely arbitrary). There were other ways the histogram could have been implemented, like using a hash table (HashMap).

For getting the median, to minimize contention, I decided to make a point-in-time copy of the histogram int array (See SnapshotReport class)

### What I learned from doing this
1. Medians are very tricky, unlike averages. It is necessary to store all the values because we need to find the middle value. However, histograms can help tremendously with calculating medians, which is how I got to my approach.

### How would I improve upon the code prior to shipping to production?
1. Think through lifecycle of startup/shutdown of MedianDB. Right now I start a PersistenceService thread to periodically persist to disk, and that is it.
2. Better error handling
3. Creating more interfaces to pass to methods, so they can be substituted out if necessary
4. Find better ways to inject configuration information into the objects
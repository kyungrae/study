# Designing Data Intensive Applications

## 1. Reliabile, Scalable, and Maintainable Application

### Reliablity

The system should continue to work correctly (perform the correct function at the desired level of performance) even in the face of adversity(hard or soft-ware faults, and event human error)

- The application can perform the function that the user expected.
- It can tolerate the user making mistakes or using the software in unexpected ways.
- Its performance is good enough for the required use case, under the expected load and data volume.
- The system prevent unauthorized access adn abuse.

### Scalability

As the system grows(in data volume, traffic volume, or complexity), there should be reasonable ways of dealing with that growth.

### Describing Load

The best choice of paramters depends on the architecture of your system.

- Requests per second
- Ratio of reads to writes
- The number of simultaneously active users in chat room
- The hit rate on a cache
- The distribution of followers per user

### Describing Performance

Once you have described the load on your system, you can investigate what happens when the load increase.

- When you increase a load parameter and keep the system resource unchanged, how is the performance of your system affected?
- When you increase a load parameter, how much do you need to increase resource if you want to keep performance unchanged?

Both questions require performance numbers, performance numbers can vary and may be values such as response time, throughput, and more.

We need to think of response time not as a single value, but as a distribution of values that you can measure.

High percentiles of response time, also known as tail latencies, are important because they directly affect user's experience of the service.

When generating load artificially in order to test the scalability of system, the load generating client needs to keep sending requests independently of the response time(queueing delay).

### Maintainability

Over time, many different people will work on the system(engineering and operations, both maintaining current behavior and adapting the system to new use case),
and they should all be able to work on in productively

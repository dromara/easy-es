> **Foreword:** ES is difficult to use, and the index bears the brunt. The creation of the index is not only complicated, but also difficult to maintain. Once the index changes, it must face the problems of service downtime and data loss caused by index reconstruction, although ES officially provides an index alias mechanism to Solving problems, but the threshold is still high, the steps are cumbersome, and manual operations are very prone to serious problems in the production environment. In order to solve these pain points, Easy-Es provides a variety of strategies to completely remove users from index maintenance. liberated from.
> Among them, the fully automatic smoothing mode adopts the world's leading "brother, you don't need to move, EE is fully automatic" mode for the first time. All the whole life cycle of index creation, update, data migration, etc. does not require user intervention, and is fully completed by EE. Zero downtime, even the index type can be intelligently and automatically inferred. It is the world's first open source initiative. It fully draws on the idea of JVM garbage collection algorithm.


**Mode 1: Smooth mode of automatic hosting (auto-block-snow mode) This mode is enabled by default (v0.9.10+ support)**

---

In this mode, users can complete the whole life cycle of index creation, update, data migration, etc. without any operation. The process has zero downtime and no user perception. It can achieve a smooth transition in the production environment, similar to the automatic transmission of a car - snow. Mode, smooth and comfortable, completely liberate users, and enjoy the fun of automatic stance!

**Mode 2: Non-smooth mode of automatic hosting (automatic gear-sport mode) (v0.9.10+ support)**

---

In this mode, the index creation and update are automatically and asynchronously completed by EE, but data migration is not handled. The speed is extremely fast, similar to the automatic transmission-sport mode of a car. Of course, if you use other tools such as logstash to synchronize data, you can also enable this mode in the production environment.

> In the above two automatic modes, the index information mainly relies on the entity class. If the user does not configure any entity class, EE can still intelligently infer the storage type of the field in ES according to the field type, which can further reduce the development cost. It is a burden for the users, and it is even more good news for Xiaobai who has just come into contact with ES.



**Mode three: manual mode (manual transmission)**

---

In this mode, the EE framework does not intervene in all maintenance work of the index, and is handled by the user. EE provides an out-of-the-box index CRUD-related API. You can choose to use this API to manually maintain the index, or use es-head to maintain the index. and other tools to maintain the index. In short, in this mode, you have a higher degree of freedom, which is more suitable for conservative users who question the EE framework or users who pursue extreme flexibility. Similar to the manual transmission of a car, it is not recommended for beginners to use this mode. , the old driver please feel free.

**Configure enable mode**

---

To configure the above three modes, you only need to add a line of configuration to your project's configuration file application.properties or application.yml:
```yaml
easy-es:
  global-config:
    process_index_mode: smoothly #smoothly: smooth mode, not_smoothly: non-smooth mode, manual: manual mode
    async-process-index-blocking: true # Whether the asynchronous processing of the index blocks the main thread is blocked by default
    distributed: false # Whether the project is deployed in a distributed environment, the default is true, if it is running on a single machine, you can fill in false, and a distributed lock will not be added, which is more efficient.
```
If this line is configured by default, smooth mode is enabled by default.

> **TIPS:**
> - When running the test module, it is strongly recommended to open the asynchronous processing index to block the main thread. Otherwise, after the test case is run, the main thread exits, but the asynchronous thread may not finish running, and a deadlock may occur. If a deadlock occurs unfortunately, delete the ee-distribute-lock That's it.
> - In the production environment or when the amount of data to be migrated is relatively large, non-blocking can be configured to enable the service to start faster.
> - For the above three modes, users can flexibly choose and experience freely according to actual needs. If you have any comments or suggestions during use, you can feedback to us, and we will continue to optimize and improve.
> - EE adopts the strategy + factory design mode in index hosting. If there are more and better modes in the future, the expansion can be easily completed without changing the original code, which conforms to the principle of open and closed. Open source enthusiasts are welcome to contribute more modes PR!
> - We will continue to uphold the concept of leaving complexity to the framework and leaving ease of use to users, and forge ahead.


# 简介
Easy-Es（简称EE）是一款基于ElasticSearch(简称Es)官方提供的RestHighLevelClient打造的低码开发框架，在 RestHighLevelClient 的基础上,只做增强不做改变，为简化开发、提高效率而生,您如果有用过Mybatis-Plus(简称MP),那么您基本可以零学习成本直接上手EE,EE是MP的Es平替版,同时也融入了更多Es独有的功能,助力您快速实现各种场景的开发.

> **理念** 把简单,易用,方便留给用户,把复杂留给框架.

> **愿景** 让天下没有难用的Es, 致力于成为全球最受欢迎的ElasticSearch搜索引擎开发框架.

# ![logo](https://iknow.hs.net/4f7ec0c1-ec40-47a6-9a69-8284cb6563a7.png)
## 优势

---

- **全自动索引托管:** 全球开源首创的索引托管模式,开发者无需关心索引的创建更新及数据迁移等繁琐步骤,索引全生命周期皆可托管给框架,由框架自动完成,过程零停机,用户无感知,彻底解放开发者
- **屏蔽语言差异:** 开发者只需要会MySQL语法即可使用Es,真正做到一通百通,无需学习枯燥易忘的Es语法,Es使用相对MySQL较低频,学了长期不用也会忘,没必要浪费这时间.开发就应该专注于业务,省下的时间去撸铁,去陪女朋友陪家人,不做资本家的韭菜
- **代码量极少:** 与直接使用RestHighLevelClient相比,相同的查询平均可以节省3-5倍左右的代码量
- **零魔法值:** 字段名称直接从实体中获取,无需输入字段名称字符串这种魔法值,提高代码可读性,杜绝因字段名称修改而代码漏改带来的Bug
- **零额外学习成本:** 开发者只要会国内最受欢迎的Mybatis-Plus语法,即可无缝迁移至EE,EE采用和前者相同的语法,消除使用者额外学习成本,直接上手,爽
- **降低开发者门槛:** Es通常需要中高级开发者才能驾驭,但通过接入EE,即便是只了解ES基础的初学者也可以轻松驾驭ES完成绝大多数需求的开发,可以提高人员利用率,降低企业成本
## 特性

---

- **无侵入**：只做增强不做改变，引入它不会对现有工程产生影响，如丝般顺滑
- **损耗小**：启动即会自动注入基本 CURD，性能基本无损耗，直接面向对象操作
- **强大的 CRUD 操作**：内置通用 Mapper，仅仅通过少量配置即可实现大部分 CRUD 操作，更有强大的条件构造器，满足各类使用需求
- **支持 Lambda 形式调用**：通过 Lambda 表达式，方便的编写各类查询条件，无需再担心字段写错段
- **支持主键自动生成**：支持2 种主键策略，可自由配置，完美解决主键问题
- **支持 ActiveRecord 模式**：支持 ActiveRecord 形式调用，实体类只需继承 Model 类即可进行强大的 CRUD 操作
- **支持自定义全局通用操作**：支持全局通用方法注入（ Write once, use anywhere ）
- **内置分页插件**：基于RestHighLevelClient 物理分页，开发者无需关心具体操作，且无需额外配置插件，写分页等同于普通 List 查询,且保持和PageHelper插件同样的分页返回字段,无需担心命名影响
- **MySQL功能全覆盖**:MySQL中支持的功能通过EE都可以轻松实现
- **支持ES高阶语法**:支持高亮搜索,分词查询,权重查询,聚合查询等高阶语法
- **良好的拓展性**:底层仍使用RestHighLevelClient,可保持其拓展性,开发者在使用EE的同时,仍可使用RestHighLevelClient的功能

...

## 框架结构

---

![EasyEsJG.jpg](https://iknow.hs.net/a60bfe0b-7b15-4cf8-9c48-51b8be94a97c.jpg)
## 代码托管

---

> [码云Gitee](https://gitee.com/dromara/easy-es)✔ | [Github](https://github.com/dromara/easy-es)✔

## English Documentation

---

[Documentation-EN](https://www.yuque.com/laohan-14b9d/tald79/qf7ns2)

## 参与贡献

---

尽管目前Easy-Es还处于新生儿状态,但由于站在巨人的肩膀上(RestHighLevelClient和Mybatis-Plus),这是一款出道即巅峰的框架,这么说并不是说它写得有多好,而是它融合了两款目前非常优秀框架的优点,这决定了它起点的高度,未来可期,所以在此欢迎各路好汉一起来参与完善 Easy-Es，我们期待你的 PR！

- 贡献代码：代码地址 [Easy-ES](https://gitee.com/dromara/easy-es)，欢迎提交 Issue 或者 Pull Requests
- 维护文档：文档地址 [Easy-ES](https://www.yuque.com/laohan-14b9d/tald79/qf7ns2)，欢迎参与翻译和修订


# 2025_se

- 可参考文件组织：
![img.png](img.png)

- mapper层可以替代dao文件夹。以下是具体说明：
- 功能等价性：二者都承担数据持久化操作职责

- dao (Data Access Object)：JPA规范中数据库操作的抽象层
- mapper：MyBatis框架中SQL映射的接口定义
技术栈对应关系：

使用JPA时 → 建议保留dao目录
使用MyBatis时 → 建议使用mapper目录
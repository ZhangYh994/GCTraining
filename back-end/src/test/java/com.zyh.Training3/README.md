## GoodManagerConcurrentTest：

此测试验证 `GoodManager` 类能否正确处理并发请求。

检查多个线程是否可以在不发生数据不一致的情况下添加和移除商品。



## GoodManagerConcurrentSequentialTest：

此测试确保并发环境中的顺序操作，并输出每步操作及`GoodManager` 当时状态。
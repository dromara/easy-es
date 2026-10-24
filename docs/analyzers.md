# Arabic、CJK 与韩语 Nori 分词器

Easy-Es 的 IndexField 接受 Elasticsearch 分析器名称字符串。以下常量为便捷别名，不会在客户端执行分词或自动安装插件。

| 常量 | Elasticsearch 名称 | 前提 |
| --- | --- | --- |
| Analyzer.ARABIC | arabic | ES 内置阿拉伯语分析器 |
| Analyzer.CJK | cjk | ES 内置中日韩基础分析器 |
| Analyzer.NORI | nori | 每个 ES 节点安装 analysis-nori 并重启 |

在实体字段上使用：

    import org.dromara.easyes.annotation.IndexField;
    import org.dromara.easyes.annotation.rely.Analyzer;
    import org.dromara.easyes.annotation.rely.FieldType;

    public class Article {
        @IndexField(fieldType = FieldType.TEXT,
                    analyzer = Analyzer.ARABIC,
                    searchAnalyzer = Analyzer.ARABIC)
        private String arabicText;

        @IndexField(fieldType = FieldType.TEXT,
                    analyzer = Analyzer.CJK,
                    searchAnalyzer = Analyzer.CJK)
        private String cjkText;

        @IndexField(fieldType = FieldType.TEXT,
                    analyzer = Analyzer.NORI,
                    searchAnalyzer = Analyzer.NORI)
        private String koreanText;
    }

CJK 提供中日韩基础处理；Nori 使用韩语形态学分析，两者不能视作完全等价替代。不要将 Nori 当成无需安装的内置分析器，也不要使用不存在的通用分析器名 korean。

Nori 由集群维护者使用与 ES 版本一致的插件安装方式配置。每个节点都需要安装 analysis-nori 并重启。Easy-Es 不执行这些运维操作。缺少插件时，Elasticsearch 可能拒绝创建使用 nori 的索引。

既有 Analyzer 常量与 IndexField 默认值均保留。自定义分析器仍可直接传入字符串。已存在索引的分析器变更需按 Elasticsearch 规则另行设计映射及重建流程；新增常量不会自动改变已有索引。

本补丁的单元测试验证常量、兼容性及注解使用，不代替真实集群的 _analyze 和分词质量验证。

参考：

- [Elasticsearch 7.17 语言分析器](https://www.elastic.co/guide/en/elasticsearch/reference/7.17/analysis-lang-analyzer.html)
- [Elasticsearch 7.17 Nori 插件](https://www.elastic.co/guide/en/elasticsearch/plugins/7.17/analysis-nori.html)
- [Easy-Es #161](https://github.com/dromara/easy-es/issues/161)

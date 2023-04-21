// 根据距离由近及远排序
wrapper.orderByDistanceAsc(boolean condition, R column, Geopoint...geoPoints);

// 根据距离由远及近排序
wrapper.orderByDistanceDesc(boolean condition, R column, Geopoint...geoPoints);

使用示例

    @Test
    public void testOrderByDistanceAsc() {
        // 测试给定中心点, 查询出中心点168.8km范围内的数据,并按照距中心点距离由近及远排序
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        GeoPoint centerPoint = new GeoPoint(41.0, 116.0);
        wrapper.match(Document::getCreator, "老汉")
                .geoDistance(Document::getLocation, 168.8, centerPoint)
                .orderByDistanceAsc(Document::getLocation, centerPoint);
        List<Document> documents = documentMapper.selectList(wrapper);
        System.out.println(documents);
    }

    @Test
    public void testOrderByDistanceAsc() {
        // 测试给定中心点, 查询出中心点168.8km范围内的数据,并按照距中心点距离由远及近排序
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        GeoPoint centerPoint = new GeoPoint(41.0, 116.0);
        wrapper.match(Document::getCreator, "老汉")
                .geoDistance(Document::getLocation, 168.8, centerPoint)
                .orderByDistanceDesc(Document::getLocation, centerPoint);
        List<Document> documents = documentMapper.selectList(wrapper);
        System.out.println(documents);
    }
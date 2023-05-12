

# Elasticsearch结合java实现组合查询  
<!-- 
Elasticsearch结合java实现组合查询（模糊，区间，精确，分页）
https://blog.csdn.net/weixin_45531476/article/details/106258777
-->


```java
@Autowired
private LogRepository logRepository;

@Override
public Map LogSearch(Integer page, Integer size, User user) {
	Integer offset = (page - 1);
	BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
	if (StringUtils.isNotBlank(user.getName)) {  
		queryBuilder.must(QueryBuilders.matchPhraseQuery("name", user.getName));
	}
	if (StringUtils.isNotBlank(user.getStarttime()) || StringUtils.isNotBlank(user.getEndtime())) {   //操作时间
		//区间查询
		queryBuilder.must(QueryBuilders.rangeQuery("timestamp.keyword").gte(user.getStarttime()).lte(user.getEndtime()));
	}
	if (StringUtils.isNotBlank(user.getFraction())) {
		//精确查询
		queryBuilder.must(QueryBuilders.termQuery("fraction", user.getFraction));
	}
	if (StringUtils.isNotBlank(user.getUsername())) {
		//模糊查询
		queryBuilder.must(QueryBuilders.matchPhraseQuery("userName", user.getUsername()));
	}
	SearchQuery searchQuery = new NativeSearchQueryBuilder()
			.withIndices("索引名称").withTypes("索引类型")
			.withQuery(queryBuilder)
			.withPageable(new PageRequest(offset, size)) //分页
			.withSort(SortBuilders.fieldSort("@timestamp").order(SortOrder.DESC))
			.build();
	Page<User> search = logRepository.search(searchQuery);
	int totalElements = (int) search.getTotalElements(); //获取总条数
	List<User> content = search.getContent();
	Map<String, Object> maps = new HashMap<>();
	maps.put("page", page); 
	maps.put("totalElements", totalElements); 
	maps.put("size", size);
	maps.put("list", content);
	return maps;
}
```



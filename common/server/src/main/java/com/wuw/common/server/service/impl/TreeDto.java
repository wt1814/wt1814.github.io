package com.wuw.common.server.service.impl;

import com.alibaba.fastjson.JSON;
import lombok.Data;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

@Data
public class TreeDto {

    private String id;
    private String name;
    private List<TreeDto> subsetTreeDtoList;



    public TreeDto(String id,String name,List<TreeDto> subsetTreeDtoList){
        this.id = id;
        this.name = name;
        this.subsetTreeDtoList = subsetTreeDtoList;
    }

    /**
     * 树形筛选查找
     * @param treeDtoList 树形集合
     * @param idList 筛选条件(可以是其他条件)
     * @return 包含的节点数据
     */
    public static List<TreeDto> screenTree(List<TreeDto> treeDtoList, List<String> idList){
        //最后返回的筛选完成的集合
        List<TreeDto> screeningOfCompleteList = new ArrayList<>();
        if (listIsNotEmpty(treeDtoList) && listIsNotEmpty(idList)){
            for (TreeDto treeDto : treeDtoList){
                List<TreeDto> subsetList = treeDto.getSubsetTreeDtoList();
                //递归筛选完成后的返回的需要添加的数据
                TreeDto addTreeDto = getSubsetPmsPlanPo(treeDto,subsetList,idList);
                if (isNotEmpty(addTreeDto)){
                    screeningOfCompleteList.add(addTreeDto);
                }
            }
            return screeningOfCompleteList;
        }
        return null;
    }


    /**
     * 筛选符合的集合并返回
     * @param treeDto 树形类
     * @param subsetTreeDtoList 子集集合
     * @param idList 筛选条件
     * @return 筛选成功的类
     */
    public static TreeDto getSubsetPmsPlanPo(TreeDto treeDto,List<TreeDto> subsetTreeDtoList,List<String> idList){
        //作为筛选条件的判断值
        String id = treeDto.getId();
        //有子集时继续向下寻找
        if (listIsNotEmpty(subsetTreeDtoList)){
            List<TreeDto> addTreeDtoList = new ArrayList<>();
            for (TreeDto subsetTreeDto : subsetTreeDtoList){
                List<TreeDto> subsetList = subsetTreeDto.getSubsetTreeDtoList();
                TreeDto newTreeDto = getSubsetPmsPlanPo(subsetTreeDto,subsetList,idList);
                //当子集筛选完不为空时添加
                if (isNotEmpty(newTreeDto)){
                    addTreeDtoList.add(newTreeDto);
                }
            }
            //子集满足条件筛选时集合不为空时，替换对象集合内容并返回当前对象
            if (listIsNotEmpty(addTreeDtoList)){
                treeDto.setSubsetTreeDtoList(addTreeDtoList);
                return treeDto;
                //当前对象子集对象不满足条件时，判断当前对象自己是否满足筛选条件，满足设置子集集合为空，并返回当前对象
            }else if (listIsEmpty(addTreeDtoList) && idList.contains(id)){
                treeDto.setSubsetTreeDtoList(null);
                return treeDto;
            }else {
                //未满足筛选条件直接返回null
                return null;
            }
        }else {
            //无子集时判断当前对象是否满足筛选条件
            if (idList.contains(id)){
                return treeDto;
            }else {
                return null;
            }
        }
    }

    /**
     * 判断集合为空
     * @param list 需要判断的集合
     * @return 集合为空时返回 true
     */
    public static boolean listIsEmpty(Collection list){
        return  (null == list || list.size() == 0);
    }

    /**
     * 判断集合非空
     * @param list 需要判断的集合
     * @return 集合非空时返回 true
     */
    public static boolean listIsNotEmpty(Collection list){
        return !listIsEmpty(list);
    }

    /**
     * 判断对象为null或空时
     * @param object 对象
     * @return 对象为空或null时返回 true
     */
    public static boolean isEmpty(Object object) {
        if (object == null) {
            return (true);
        }
        if ("".equals(object)) {
            return (true);
        }
        if ("null".equals(object)) {
            return (true);
        }
        return (false);
    }

    /**
     * 判断对象非空
     * @param object 对象
     * @return 对象为非空时返回 true
     */
    public static boolean isNotEmpty(Object object) {
        if (object != null && !object.equals("") && !object.equals("null")) {
            return (true);
        }
        return (false);
    }

    public static void main(String[] args) {
        TreeDto treeDto1 = new TreeDto("1","A",new ArrayList<TreeDto>());
        TreeDto treeDto1_1 = new TreeDto("1.1","A-A",new ArrayList<TreeDto>());
        TreeDto treeDto1_2 = new TreeDto("1.2","A-B",new ArrayList<TreeDto>());
        TreeDto treeDto1_3 = new TreeDto("1.3","A-C",new ArrayList<TreeDto>());
        treeDto1.getSubsetTreeDtoList().add(treeDto1_1);
        treeDto1.getSubsetTreeDtoList().add(treeDto1_2);
        treeDto1.getSubsetTreeDtoList().add(treeDto1_3);

        TreeDto treeDto2 = new TreeDto("2","B",new ArrayList<TreeDto>());
        TreeDto treeDto2_1 = new TreeDto("2.1","B-A",new ArrayList<TreeDto>());
        TreeDto treeDto2_2 = new TreeDto("2.2","B-B",new ArrayList<TreeDto>());
        TreeDto treeDto2_3 = new TreeDto("2.3","B-C",new ArrayList<TreeDto>());
        TreeDto treeDto2_3_1 = new TreeDto("2.3.1","B-C-A",null);
        treeDto2.getSubsetTreeDtoList().add(treeDto2_1);
        treeDto2.getSubsetTreeDtoList().add(treeDto2_2);
        treeDto2.getSubsetTreeDtoList().add(treeDto2_3);
        treeDto2.getSubsetTreeDtoList().get(2).getSubsetTreeDtoList().add(treeDto2_3_1);

        String[] array = {"1.3","2.2","2.3.1"};
        List<String> idList = Arrays.asList(array);
        List<TreeDto> treeDtoList = new ArrayList<>();
        treeDtoList.add(treeDto1);
        treeDtoList.add(treeDto2);
        System.out.println(JSON.toJSONString(screenTree(treeDtoList,idList)));
    }
}


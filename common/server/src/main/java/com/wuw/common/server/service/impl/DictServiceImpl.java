package com.wuw.common.server.service.impl;

import com.wuw.common.server.dao.DictMapper;
import com.wuw.common.server.model.VO.DictRequestVo;
import com.wuw.common.server.model.VO.DictResponseVo;
import com.wuw.common.server.service.DictService;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Service
public class DictServiceImpl implements DictService{

    @Resource
    private DictMapper dictMapper;

    @Override
    public List<DictResponseVo> findDictsByCode(DictRequestVo dictRequestVo) {

        // 1. 查询全量树行结构，除了最顶级
        List<DictResponseVo> dictResponseVos = dictMapper.selectByParentCode("zero");

        // 2.1. todo 筛选条件不为空，获取父级【从下往上】
        if (null != dictResponseVos && dictResponseVos.size() >0 && StringUtils.isNotBlank(dictRequestVo.getDictName())){
            dictResponseVos = conditionalFilter(dictResponseVos,dictRequestVo);
        }

        // 2.2. todo parentCode不为空，获取子集【从上往下】
        if(StringUtils.isNotBlank(dictRequestVo.getParentCode())){
            // 1. 加入最顶级，重新构造树
            List<DictResponseVo> dictResponseVos1 = new ArrayList<>();
            DictResponseVo dictResponseVo = new DictResponseVo();
            dictResponseVo.setDictCode("zero");
            dictResponseVo.setDictVos(dictResponseVos);
            dictResponseVos1.add(dictResponseVo);
            // 2. 递归向下查询
            for (DictResponseVo dictResponseVo1:dictResponseVos1){
                dictResponseVos = findChildsByParentCode(dictResponseVo1,dictResponseVo1.getDictVos(),dictRequestVo.getParentCode());
                if (dictResponseVos != null) {
                    return dictResponseVos;
                }
            }

        }

        return dictResponseVos;

    }


    //////////////////////////////////////////////////////////获取子集

    /**
     *
     * @param dictResponseVo1 父级
     * @param dictVos 子集
     * @param parentCode 需要匹配的code
     * @return
     */
    private List<DictResponseVo> findChildsByParentCode(DictResponseVo dictResponseVo1, List<DictResponseVo> dictVos, String parentCode) {
        if (dictResponseVo1.getDictCode().equals(parentCode)){
            return dictVos;
        }

        for (DictResponseVo dictResponseVo:dictVos){
            List<DictResponseVo> childsByParentCode = findChildsByParentCode(dictResponseVo, dictResponseVo.getDictVos(), parentCode);
            if (null != childsByParentCode){
                return childsByParentCode;
            }
        }
        return  null;

    }

    ///////////////////////////////////////////////////////////获取父级
    // todo Java树形结构筛选 https://blog.csdn.net/cyunwli/article/details/124452562
    /**
     *
     * @param dictResponseVos
     * @param dictRequestVo
     * @return
     */
    public List<DictResponseVo> conditionalFilter(List<DictResponseVo> dictResponseVos, DictRequestVo dictRequestVo){
        List<DictResponseVo> responseVos = new ArrayList<>();
        for (DictResponseVo dictResponseVo:dictResponseVos){
            List<DictResponseVo> dictVos = dictResponseVo.getDictVos(); // 获取子集
            DictResponseVo dictResponse = getSubsetPmsPlanPo(dictResponseVo,dictVos,dictRequestVo);
            if (null != dictResponse){
                responseVos.add(dictResponse);
            }
        }
        return responseVos;
    }

    /**
     *
     * @param dictResponseVo
     * @param childDictResponseVos
     * @param dictRequestVo
     * @return
     */
    private DictResponseVo getSubsetPmsPlanPo(DictResponseVo dictResponseVo, List<DictResponseVo> childDictResponseVos, DictRequestVo dictRequestVo) {

        List<DictResponseVo> dictResponseVoList = new ArrayList<>();
        //有子集时继续向下寻找
        if (null != childDictResponseVos && childDictResponseVos.size() >0){

            for (DictResponseVo dictResponseVo1:childDictResponseVos){
                List<DictResponseVo> dictVos1 = dictResponseVo1.getDictVos();
                DictResponseVo subsetPmsPlanPo = getSubsetPmsPlanPo(dictResponseVo1, dictVos1, dictRequestVo);
                if (null != subsetPmsPlanPo){
                    //当子集筛选完不为空时添加
                    dictResponseVoList.add(subsetPmsPlanPo);
                }
            }
            // 子集满足条件筛选时集合不为空时，替换对象集合内容并返回当前对象
            if (dictResponseVoList.size() != 0){
                dictResponseVo.setDictVos(dictResponseVoList);
                return dictResponseVo;
            }
            //当前对象子集对象不满足条件时，判断当前对象自己是否满足筛选条件，满足设置子集集合为空，并返回当前对象
            else if (dictResponseVoList.size() == 0 ){
                // todo 条件判断
                if (dictRequestVo.getDictName().equals(dictResponseVo.getDictName())){
                    dictResponseVo.setDictVos(dictResponseVoList);
                    return dictResponseVo;
                }
            }
            // 未满足筛选条件直接返回null
            else {
                return null;
            }

        }
        //无子集时判断当前对象是否满足筛选条件
        else {
            // todo 条件判断
            if (dictRequestVo.getDictName().equals(dictResponseVo.getDictName())){
                return dictResponseVo;
            }else {
                return null;
            }

        }
        return null;
    }


}

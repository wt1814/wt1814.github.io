package com.souche.serivce.integration.service.impl;

import com.alibaba.dubbo.config.annotation.Reference;
import com.souche.auction.tag.api.TagAuctionRemoteService;
import com.souche.auction.tag.dto.ActivityAuctionRelationInfoDTO;
import com.souche.marketing.api.model.vo.AdvertPlaceAppVO;
import com.souche.marketing.api.service.AdvertPlaceNewServiceApi;
import com.souche.service.integration.model.dto.AdvertPlaceAppDTO;
import com.souche.service.integration.service.AdvertPlaceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @ProjectName：platform-serivce-integration
 * @ClassName: AdvertPlaceServiceImpl
 * @Description: 运营位 
 * @Author: wt 
 * @CreateDate: 2021-01-07 18:04
 * @UpdateUser: 
 * @UpdateDate:   
 * @UpdateRemark:
 * @Version: V1.0
 **/
@Slf4j
@Service
public class AdvertPlaceServiceImpl implements AdvertPlaceService {

    @Reference
    AdvertPlaceNewServiceApi advertPlaceNewServiceApi;
    @Reference
    TagAuctionRemoteService tagAuctionRemoteService;

    /**
     * 线上运营位
     * @param token
     * @param advertRange
     * @param city
     * @return
     */
    @Override
    public List<AdvertPlaceAppDTO> onlineAdvertPlaces(String token, String advertRange, String city) {
        List<AdvertPlaceAppDTO> advertPlaceAppDTOS = new ArrayList<>();
        List<AdvertPlaceAppVO> advertPlaceAppVOS = advertPlaceNewServiceApi.onlineAdvertPlaces(token, advertRange, city);
        if (null == advertPlaceAppVOS || advertPlaceAppVOS.size() == 0){
            log.error("获取广告位数目0");
            return null;
        }
        getAppDTO(advertPlaceAppDTOS, advertPlaceAppVOS);
        return advertPlaceAppDTOS;
    }


    /**
     * 预展运营位
     * @param advertRange
     * @return
     */
    @Override
    public List<AdvertPlaceAppDTO> previewAdvertPlaces(String advertRange) {
        List<AdvertPlaceAppDTO> advertPlaceAppDTOS = new ArrayList<>();
        List<AdvertPlaceAppVO> advertPlaceAppVOS = advertPlaceNewServiceApi.previewAdvertPlaces(advertRange);
        if (null == advertPlaceAppVOS || advertPlaceAppVOS.size() == 0){
            return null;
        }
        getAppDTO(advertPlaceAppDTOS, advertPlaceAppVOS);
        return advertPlaceAppDTOS;
    }



    private void getAppDTO(List<AdvertPlaceAppDTO> advertPlaceAppDTOS, List<AdvertPlaceAppVO> advertPlaceAppVOS) {
        AdvertPlaceAppDTO advertPlaceAppDTO;
        List<Long> activityIds = advertPlaceAppVOS.stream().map(AdvertPlaceAppVO::getCActivityID).collect(Collectors.toList());
        List<ActivityAuctionRelationInfoDTO> activityAuctionRelationInfos = tagAuctionRemoteService.getActivityInfoByIds(activityIds);

        for (AdvertPlaceAppVO advertPlaceAppVO:advertPlaceAppVOS){
            for (ActivityAuctionRelationInfoDTO activityAuctionRelationInfoDTO:activityAuctionRelationInfos){
                if (advertPlaceAppVO.getCActivityID() == activityAuctionRelationInfoDTO.getId()){
                    advertPlaceAppDTO = new AdvertPlaceAppDTO();
                    BeanUtils.copyProperties(advertPlaceAppVO,advertPlaceAppDTO);
                    Integer auctionStatus = activityAuctionRelationInfoDTO.getAuctionInfoDTOS().get(0).getAuctionStatus();
                    String status = "";
                    if (auctionStatus == 3||auctionStatus ==30||auctionStatus ==4||auctionStatus ==5||auctionStatus ==8||auctionStatus ==80||auctionStatus ==81||auctionStatus ==9){
                        status = "竞拍中";
                    }else if (auctionStatus == 1||auctionStatus ==2){
                        status = "预展中";
                    }
                    advertPlaceAppDTO.setStatus(status);
                    advertPlaceAppDTO.setDate(activityAuctionRelationInfoDTO.getAuctionInfoDTOS().get(0).getBeginTime());
                    List<String> carImgPaths = new ArrayList<>();
                    String carImgPath = activityAuctionRelationInfoDTO.getAuctionInfoDTOS().get(0).getFirstImage();
                    carImgPaths.add(carImgPath);
                    if (activityAuctionRelationInfoDTO.getAuctionInfoDTOS().get(1) != null){
                        String carImgPath1 = activityAuctionRelationInfoDTO.getAuctionInfoDTOS().get(1).getFirstImage();
                        carImgPaths.add(carImgPath1);
                    }
                    advertPlaceAppDTO.setCarImgPaths(carImgPaths);
                    advertPlaceAppDTOS.add(advertPlaceAppDTO);
                }
            }
        }
    }
}
package com.souche.serivce.integration.service.impl;

        import com.alibaba.dubbo.config.annotation.Reference;
        import com.souche.auction.tag.api.TagAuctionRemoteService;
        import com.souche.auction.tag.dto.ActivityAuctionRelationInfoDTO;
        import com.souche.marketing.api.model.vo.AdvertPlaceAppVO;
        import com.souche.marketing.api.service.AdvertPlaceNewServiceApi;
        import com.souche.service.integration.model.dto.AdvertPlaceAppDTO;
        import com.souche.service.integration.service.AdvertPlaceService;
        import lombok.extern.slf4j.Slf4j;
        import org.springframework.beans.BeanUtils;
        import org.springframework.stereotype.Service;

        import java.util.ArrayList;
        import java.util.List;
        import java.util.stream.Collectors;

/**
 * @ProjectName：platform-serivce-integration
 * @ClassName: AdvertPlaceServiceImpl
 * @Description: 运营位 
 * @Author: wt 
 * @CreateDate: 2021-01-07 18:04
 * @UpdateUser: 
 * @UpdateDate:   
 * @UpdateRemark:
 * @Version: V1.0
 **/
@Slf4j
@Service
public class AdvertPlaceServiceImpl implements AdvertPlaceService {

    @Reference
    AdvertPlaceNewServiceApi advertPlaceNewServiceApi;
    @Reference
    TagAuctionRemoteService tagAuctionRemoteService;

    /**
     * 线上运营位
     * @param token
     * @param advertRange
     * @param city
     * @return
     */
    @Override
    public List<AdvertPlaceAppDTO> onlineAdvertPlaces(String token, String advertRange, String city) {
        List<AdvertPlaceAppDTO> advertPlaceAppDTOS = new ArrayList<>();
        List<AdvertPlaceAppVO> advertPlaceAppVOS = advertPlaceNewServiceApi.onlineAdvertPlaces(token, advertRange, city);
        if (null == advertPlaceAppVOS || advertPlaceAppVOS.size() == 0){
            log.error("获取广告位数目0");
            return null;
        }
        getAppDTO(advertPlaceAppDTOS, advertPlaceAppVOS);
        return advertPlaceAppDTOS;
    }


    /**
     * 预展运营位
     * @param advertRange
     * @return
     */
    @Override
    public List<AdvertPlaceAppDTO> previewAdvertPlaces(String advertRange) {
        List<AdvertPlaceAppDTO> advertPlaceAppDTOS = new ArrayList<>();
        List<AdvertPlaceAppVO> advertPlaceAppVOS = advertPlaceNewServiceApi.previewAdvertPlaces(advertRange);
        if (null == advertPlaceAppVOS || advertPlaceAppVOS.size() == 0){
            return null;
        }
        getAppDTO(advertPlaceAppDTOS, advertPlaceAppVOS);
        return advertPlaceAppDTOS;
    }



    private void getAppDTO(List<AdvertPlaceAppDTO> advertPlaceAppDTOS, List<AdvertPlaceAppVO> advertPlaceAppVOS) {
        AdvertPlaceAppDTO advertPlaceAppDTO;
        List<Long> activityIds = advertPlaceAppVOS.stream().map(AdvertPlaceAppVO::getCActivityID).collect(Collectors.toList());
        List<ActivityAuctionRelationInfoDTO> activityAuctionRelationInfos = tagAuctionRemoteService.getActivityInfoByIds(activityIds);

        for (AdvertPlaceAppVO advertPlaceAppVO:advertPlaceAppVOS){
            for (ActivityAuctionRelationInfoDTO activityAuctionRelationInfoDTO:activityAuctionRelationInfos){
                if (advertPlaceAppVO.getCActivityID() == activityAuctionRelationInfoDTO.getId()){
                    advertPlaceAppDTO = new AdvertPlaceAppDTO();
                    BeanUtils.copyProperties(advertPlaceAppVO,advertPlaceAppDTO);
                    Integer auctionStatus = activityAuctionRelationInfoDTO.getAuctionInfoDTOS().get(0).getAuctionStatus();
                    String status = "";
                    if (auctionStatus == 3||auctionStatus ==30||auctionStatus ==4||auctionStatus ==5||auctionStatus ==8||auctionStatus ==80||auctionStatus ==81||auctionStatus ==9){
                        status = "竞拍中";
                    }else if (auctionStatus == 1||auctionStatus ==2){
                        status = "预展中";
                    }
                    advertPlaceAppDTO.setStatus(status);
                    advertPlaceAppDTO.setDate(activityAuctionRelationInfoDTO.getAuctionInfoDTOS().get(0).getBeginTime());
                    List<String> carImgPaths = new ArrayList<>();
                    String carImgPath = activityAuctionRelationInfoDTO.getAuctionInfoDTOS().get(0).getFirstImage();
                    carImgPaths.add(carImgPath);
                    if (activityAuctionRelationInfoDTO.getAuctionInfoDTOS().get(1) != null){
                        String carImgPath1 = activityAuctionRelationInfoDTO.getAuctionInfoDTOS().get(1).getFirstImage();
                        carImgPaths.add(carImgPath1);
                    }
                    advertPlaceAppDTO.setCarImgPaths(carImgPaths);
                    advertPlaceAppDTOS.add(advertPlaceAppDTO);
                }
            }
        }
    }
}

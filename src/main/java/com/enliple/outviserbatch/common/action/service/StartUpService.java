package com.enliple.outviserbatch.common.action.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.enliple.outviserbatch.common.action.mapper.StartupMapper;
import com.enliple.outviserbatch.common.data.DataMap;

@Repository
public class StartUpService {
    
    
    @Autowired
    private StartupMapper startupMapper;
    
    /**
     * 코드를 가져온다
     * @param param
     * @return
     * @throws Exception
     */
    public List<DataMap> selectCodeList() throws Exception{
    	return startupMapper.selectCodeList();
    }
    
    /**
     * 캠페인 조건별 난이도 점수 가져오기
     * @return
     * @throws Exception
     */
    public DataMap selectCampCondScoreList() throws Exception{
    	DataMap score = new DataMap();
    	List<DataMap> scoreList = startupMapper.selectCampCondScoreList();
    	for (DataMap scoreMap : scoreList) {
    		score.put(scoreMap.getString("COND_KEY"), scoreMap.getInt("SCORE"));
    	}
    	
    	return score;
    }

}
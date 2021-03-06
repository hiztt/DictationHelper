package com.dictation.back.service.impl;

import com.dictation.back.entity.Back;
import com.dictation.back.service.BackService;
import com.dictation.mapper.BackMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @ClassName BackServiceImpl
 * @Description
 * @Author zlc
 * @Date 2020-04-13 11:28
 */
@Service("backService")
public class BackServiceImpl implements BackService {

    @Autowired
    BackMapper backMapper;

    @Override
    public Back saveOne(Back back) {
        if(backMapper.insert(back) == 1) return back;
        return null;
    }

    @Override
    public boolean deleteById(Integer id) {
        if(backMapper.deleteById(id) == 1) return true;
        return false;
    }

    @Override
    public boolean delete(Back back) {
        if(backMapper.deleteById(back.getId()) == 1) return true;
        return false;
    }

    @Override
    public boolean updateOne(Back back) {
        if(backMapper.updateById(back) == 1) return true;
        return false;
    }

    @Override
    public Back findById(Integer id) {
        return backMapper.selectById(id);
    }

    @Override
    public List<Back> findAll() {
        return backMapper.selectList(null);
    }


}

package com.koyokoyo.community.community.dao;

import org.springframework.stereotype.Repository;

@Repository("AlphaHib")
public class AlphaDaoHibernateImp implements AlphaDao {

    @Override
    public String select() {
        return "Hibernate";
    }
}

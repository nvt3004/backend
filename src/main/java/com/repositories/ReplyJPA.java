package com.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.entities.Reply;

public interface ReplyJPA extends JpaRepository<Reply, Integer> {

}

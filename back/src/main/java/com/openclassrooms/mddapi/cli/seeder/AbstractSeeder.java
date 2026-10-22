package com.openclassrooms.mddapi.cli.seeder;

import lombok.extern.log4j.Log4j2;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Log4j2
public abstract class AbstractSeeder<T> implements Seeder {

    protected final JpaRepository<T, ?> repository;

    protected AbstractSeeder(JpaRepository<T, ?> repository) {
        this.repository = repository;
        log.info("Constructor - repository: {}", repository);
    }

    @Override
    @Transactional
    public void clear() {
        log.info("Clearing {}", repository);
        repository.deleteAll();
    }

    @Override
    @Transactional
    public void seed() {
        log.info("Seeding {}", repository);
        repository.saveAll(getEntities());
    }

    protected abstract List<T> getEntities();
}

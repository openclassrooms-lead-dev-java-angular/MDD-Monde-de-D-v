package com.openclassrooms.mddapi.cli.seeder;

import com.openclassrooms.mddapi.topic.entity.Topic;
import com.openclassrooms.mddapi.topic.repository.TopicRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

import java.util.List;

@Log4j2
@Component
public class TopicSeeder extends AbstractSeeder<Topic> {

    public TopicSeeder(TopicRepository topicRepository) {
        super(topicRepository);
        log.info("TopicSeeder repository: {}", topicRepository);
    }

    @Override
    protected List<Topic> getEntities() {
        List<String> languages = List.of(
                "Java", "TypeScript", "JavaScript", "Cobol", "Python"
        );

        return languages.stream()
                .map(language -> {
                    Topic topic = new Topic();
                    topic.setName(language);
                    topic.setSlug(language);
                    topic.setDescription(language + " development.");
                    return topic;
                })
                .toList();
    }
}

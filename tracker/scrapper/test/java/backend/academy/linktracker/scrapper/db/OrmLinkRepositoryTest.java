package backend.academy.linktracker.scrapper.db;

import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = "app.access-type=orm")
public class OrmLinkRepositoryTest extends AbstractLinkRepositoryTest {}

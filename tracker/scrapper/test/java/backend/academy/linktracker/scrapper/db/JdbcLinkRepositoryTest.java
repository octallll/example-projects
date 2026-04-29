package backend.academy.linktracker.scrapper.db;

import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = "app.access-type=sql")
class JdbcLinkRepositoryTest extends AbstractLinkRepositoryTest {}

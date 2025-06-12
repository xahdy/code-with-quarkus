import com.mongodb.kotlin.client.coroutine.MongoClient
import jakarta.enterprise.context.Dependent
import jakarta.enterprise.inject.Produces
import jakarta.inject.Singleton
import org.acme.MongoClientConfig

@Dependent
class MongoDBClientProducer(
    private val config: MongoClientConfig
) {

    @Produces
    @Singleton
    fun createClient() = MongoClient.create(
        connectionString = config.connectionString()
    )

    @Produces
    @Singleton
    fun createDatabase(client: MongoClient) = client.getDatabase(
        databaseName = config.database()
    )
}
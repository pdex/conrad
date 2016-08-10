package pdex.conrad;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.collection.CollectRequest;
import org.eclipse.aether.collection.CollectResult;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.graph.DependencyFilter;
import org.eclipse.aether.util.artifact.JavaScopes;
import org.eclipse.aether.util.filter.DependencyFilterUtils;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class Tree {

  public static void main( String[] args ) throws Exception {
    RepositorySystem system = Booter.newRepositorySystem();

    RepositorySystemSession session = Booter.newRepositorySystemSession( system, System.err );

    Config dependencies = ConfigFactory.load().getConfig("conrad.dependencies");

    List<Artifact> artifacts = new ArrayList<Artifact>();
    dependencies.root().entrySet().stream().forEach((item) -> {
      log.debug("item {}: {}", item.getKey(), item.getValue());
      Object value = item.getValue().unwrapped();
      if (value instanceof String) {
        artifacts.add(new DefaultArtifact((String)value));
      }
    });

    DependencyFilter classpathFlter = DependencyFilterUtils.classpathFilter( JavaScopes.COMPILE );

    CollectRequest collectRequest = new CollectRequest();

    for (Artifact artifact : artifacts) {
      collectRequest.addDependency(new Dependency(artifact, JavaScopes.COMPILE));
    }

    collectRequest.setRepositories( Booter.newRepositories( system, session ) );

    CollectResult collectResult = system.collectDependencies( session, collectRequest );

    collectResult.getRoot().accept( new ConsoleDependencyGraphDumper() );
  }
}

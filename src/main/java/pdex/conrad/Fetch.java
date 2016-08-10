package pdex.conrad;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.collection.CollectRequest;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.graph.DependencyFilter;
import org.eclipse.aether.resolution.ArtifactResult;
import org.eclipse.aether.resolution.DependencyRequest;
import org.eclipse.aether.util.artifact.JavaScopes;
import org.eclipse.aether.util.filter.DependencyFilterUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Resolves the transitive (compile) dependencies of an artifact.
 */
@Slf4j
public class Fetch {

  public static void main( String[] args ) throws Exception {
    log.debug( "------------------------------------------------------------" );
    log.debug( "Fetch" );

    RepositorySystem system = Booter.newRepositorySystem();

    RepositorySystemSession session = Booter.newRepositorySystemSession( system, System.err );

    Config dependencies = ConfigFactory.load().getConfig("conrad.dependencies");
    //System.out.println("deps: " + dependencies);
    List<Artifact> artifacts = new ArrayList<Artifact>();
    dependencies.root().entrySet().stream().forEach((item) -> {
      log.debug("item {}: {}", item.getKey(), item.getValue());
      Object value = item.getValue().unwrapped();
      if (value instanceof String) {
        //System.out.println("value is string");
        artifacts.add(new DefaultArtifact((String)value));
      }
      //String artifactCoords = dependencies.getString(item.getKey());
    });

    //Artifact artifact = new DefaultArtifact( "org.eclipse.aether:aether-impl:1.0.0.v20140518" );

    DependencyFilter classpathFlter = DependencyFilterUtils.classpathFilter( JavaScopes.COMPILE );

    CollectRequest collectRequest = new CollectRequest();
    //    collectRequest.setRoot( new Dependency( artifact, JavaScopes.COMPILE ) );
    for (Artifact artifact : artifacts) {
      collectRequest.addDependency(new Dependency(artifact, JavaScopes.COMPILE));
    }

    collectRequest.setRepositories( Booter.newRepositories( system, session ) );

    DependencyRequest dependencyRequest = new DependencyRequest( collectRequest, classpathFlter );

    List<ArtifactResult> artifactResults =
      system.resolveDependencies( session, dependencyRequest ).getArtifactResults();

    for ( ArtifactResult artifactResult : artifactResults ) {
      log.debug("{} resolved to {}", artifactResult.getArtifact(), artifactResult.getArtifact().getFile() );
    }

    List<String> jars = new ArrayList<String>();
    String classpath = artifactResults.stream().map((a) -> a.getArtifact().getFile().getPath()).collect(Collectors.joining(":"));

    System.out.println(classpath.replace(System.getProperty("user.home"), "${HOME}"));

  }
}

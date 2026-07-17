package igentuman.nc.content.particles;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * source https://github.com/Lach01298/QMD
 */
public interface ITileParticleStorage
{
	public @Nonnull List<? extends ParticleStorage> getParticleBeams();
}

package dev.atcrock.totemfx.client;

import dev.atcrock.totemfx.TotemFX;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.particle.DustColorTransitionParticleEffect;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class TotemFXClient implements ClientModInitializer {

    private static class Beam {
        Vec3d start;
        Vec3d end;
        int age;
        int lifetime;
        float r1, g1, b1, r2, g2, b2;
        public Beam(Vec3d start, Vec3d end, int lifetime, Random rand) {
            this.start = start;
            this.end = end;
            this.age = 0;
            this.lifetime = lifetime;
            float h1 = rand.nextFloat();
            float h2 = (h1 + 0.2f + rand.nextFloat()*0.2f) % 1f;
            float[] c1 = java.awt.Color.getHSBColor(h1, 1f, 1f).getRGBColorComponents(null);
            float[] c2 = java.awt.Color.getHSBColor(h2, 1f, 1f).getRGBColorComponents(null);
            r1=c1[0]; g1=c1[1]; b1=c1[2];
            r2=c2[0]; g2=c2[1]; b2=c2[2];
        }
    }

    private static class FXState {
        int ticks;
        UUID who;
        List<Beam> beams = new ArrayList<>();
        Random rand = new Random();
    }

    private static FXState state = null;

    @Override
    public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(TotemFX.START_FX, (client, handler, buf, responseSender) -> {
            UUID who = buf.readUuid();
            client.execute(() -> {
                state = new FXState();
                state.ticks = 0;
                state.who = who;
                state.beams.clear();
                ClientPlayerEntity p = MinecraftClient.getInstance().player;
                if (p != null) {
                    Vec3d center = p.getPos().add(0, 1.0, 0);
                    for (int i = 0; i < 72; i++) {
                        double radius = 20 + state.rand.nextDouble() * 10.0;
                        double theta = 2*Math.PI * i / 72.0;
                        double phi = state.rand.nextDouble() * Math.PI;
                        Vec3d dir = new Vec3d(MathHelper.sin((float)phi)*Math.cos(theta),
                                              MathHelper.cos((float)phi),
                                              MathHelper.sin((float)phi)*Math.sin(theta));
                        Vec3d start = center.add(dir.multiply(radius));
                        state.beams.add(new Beam(start, center, 40, state.rand));
                    }
                }
            });
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.world == null || state == null) return;
            ClientPlayerEntity p = client.player;
            if (p == null || !p.getUuid().equals(state.who)) return;
            Vec3d center = p.getPos().add(0, 1.0, 0);
            int t = state.ticks;

            if (t < 40) {
                for (Beam b : state.beams) {
                    double progress = (double)b.age / (double)b.lifetime;
                    Vec3d point = b.start.lerp(b.end, progress);
                    Vec3d next = b.start.lerp(b.end, Math.min(1.0, progress + 0.08));
                    Vec3d seg = next.subtract(point);
                    int steps = 8;
                    for (int i=0;i<=steps;i++) {
                        Vec3d pos = point.add(seg.multiply(i/(double)steps));
                        ParticleEffect eff = new DustColorTransitionParticleEffect(
                            b.r1, b.g1, b.b1, 1.0f,
                            b.r2, b.g2, b.b2, 1.0f, 1.2f
                        );
                        client.world.addParticle(eff, pos.x, pos.y, pos.z, 0, 0, 0);
                    }
                    b.age++;
                }
            }

            if (t >= 40 && t < 60) {
                float orbR=0.82f, orbG=0.0f, orbB=0.95f;
                float radius = 0.5f + (float)Math.sin((t-40)/20.0*Math.PI)*1.3f;
                int ringPoints = 60;
                for (int i=0;i<ringPoints;i++) {
                    double a = 2*Math.PI*i/ringPoints;
                    double rx = Math.cos(a)*radius;
                    double rz = Math.sin(a)*radius;
                    client.world.addParticle(new DustParticleEffect(orbR,orbG,orbB,1.0f),
                        center.x+rx, center.y, center.z+rz, 0,0,0);
                    client.world.addParticle(new DustParticleEffect(orbR,orbG,orbB,1.0f),
                        center.x+rx*0.6, center.y+0.6, center.z+rz*0.6, 0,0,0);
                }
            }

            if (t >= 60 && t <= 100) {
                float r=1.0f, g=0.2f, b=0.95f;
                int layers = 3;
                for (int L=0; L<layers; L++) {
                    double ringRadius = (t-60)/40.0 * (6 + L*2);
                    int points = 80;
                    for (int i=0;i<points;i++) {
                        double a = 2*Math.PI*i/points + L*0.6 + t*0.1;
                        double x = center.x + Math.cos(a)*ringRadius;
                        double z = center.z + Math.sin(a)*ringRadius;
                        double y = center.y + Math.sin(a*2 + L)*0.7;
                        client.world.addParticle(new DustParticleEffect(r,g,b,1.0f), x, y, z, 0,0,0);
                    }
                }
            }

            state.ticks++;
            if (state.ticks > 100) state = null;
        });
    }
}

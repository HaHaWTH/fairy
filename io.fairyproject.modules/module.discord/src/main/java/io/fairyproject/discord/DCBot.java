package io.fairyproject.discord;

import io.fairyproject.container.*;
import io.fairyproject.discord.button.ButtonReader;
import io.fairyproject.discord.message.NextMessageReader;
import io.fairyproject.discord.proxies.ProxyJDA;
import io.fairyproject.metadata.MetadataMap;
import io.fairyproject.reflect.Reflect;
import io.fairyproject.util.PreProcessBatch;
import lombok.Getter;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.events.Event;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

import javax.security.auth.login.LoginException;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@AnnotateSuggest("io.fairyproject.discord.DCIndex")
@Getter
public abstract class DCBot implements ProxyJDA {

    private static final Map<Long, DCBot> BOT_BY_ID = new ConcurrentHashMap<>();

    public static Collection<DCBot> all() {
        return BOT_BY_ID.values();
    }

    public static DCBot from(long id) {
        return BOT_BY_ID.get(id);
    }

    public static DCBot from(JDA jda) {
        return BOT_BY_ID.get(jda.getSelfUser().getIdLong());
    }

    public static DCBot from(Event event) {
        return BOT_BY_ID.get(event.getJDA().getSelfUser().getIdLong());
    }

    protected JDA jda;
    protected String index;
    protected Mode mode;
    protected NextMessageReader nextMessageReader;
    protected ButtonReader buttonReader;

    private PreProcessBatch listenerBatch;
    private MetadataMap metadata;

    @Override
    public final JDA getJDA() {
        assert this.jda != null;
        return this.jda;
    }

    public final MetadataMap metadata() {
        return this.metadata;
    }

    @PreInitialize
    public void onPreInitialize() {
        this.listenerBatch = PreProcessBatch.create();
        this.metadata = MetadataMap.create();
    }

    @PostInitialize
    public void onPostInitialize() throws LoginException {
        this.index = Reflect.getAnnotationValue(this.getClass(), DCIndex.class, a -> a != null ? a.value() : this.getClass().getName());
        this.mode = Reflect.getAnnotationValue(this.getClass(), DCMode.class, a -> a != null ? a.value() : Mode.DEFAULT);

        JDABuilder jdaBuilder;
        switch (this.mode) {
            case LIGHT:
                jdaBuilder = JDABuilder.createLight(this.createToken());
                break;
            case DEFAULT:
            default:
                jdaBuilder = JDABuilder.createDefault(this.createToken());
                break;
        }

        jdaBuilder = this.setupBuilder(jdaBuilder);
        this.jda = jdaBuilder.build();
        this.listenerBatch.flushQueue();
        this.nextMessageReader = new NextMessageReader(this);
        this.buttonReader = new ButtonReader();

        BOT_BY_ID.put(this.jda.getSelfUser().getIdLong(), this);
    }

    @PreDestroy
    public void onDestroy() {
        BOT_BY_ID.remove(this.jda.getSelfUser().getIdLong());

        for (Object registeredListener : this.jda.getEventManager().getRegisteredListeners()) {
            this.jda.removeEventListener(registeredListener);
        }
        this.jda.shutdown();
    }

    @Override
    public void addEventListener(Object... objs) {
        for (Object obj : objs) {
            final Class<?> aClass = obj.getClass();
            String index = Reflect.getAnnotationValueOrNull(aClass, DCIndex.class, DCIndex::value);
            if (index != null && !index.equals(this.index)) {
                return;
            }
            this.listenerBatch.runOrQueue(aClass.getName(), () -> this.jda.addEventListener(obj));
        }
    }

    @Override
    public void removeEventListener(Object... objs) {
        for (Object obj : objs) {
            final Class<?> aClass = obj.getClass();
            String index = Reflect.getAnnotationValueOrThrow(aClass, DCIndex.class, DCIndex::value);
            if (!index.equals(this.index)) {
                return;
            }
            if (!this.listenerBatch.remove(obj.getClass().getName())) {
                this.jda.removeEventListener(obj);
            }
        }
    }

    protected JDABuilder setupBuilder(JDABuilder jdaBuilder) {
        return jdaBuilder
                .disableCache(CacheFlag.ACTIVITY)
                .setMemberCachePolicy(MemberCachePolicy.ALL)
                .setChunkingFilter(ChunkingFilter.ALL)
                .enableIntents(EnumSet.allOf(GatewayIntent.class));
    }

    protected abstract String createToken();

    public enum Mode {
        LIGHT,
        DEFAULT
    }

}

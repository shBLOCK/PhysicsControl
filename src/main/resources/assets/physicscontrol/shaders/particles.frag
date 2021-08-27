#version 430

uniform uint count;
uniform float size;
uniform vec2 screenSize;
uniform vec2 translate;
uniform float scale;
uniform samplerBuffer data;

uniform float smoothLower;
uniform float smoothUpper;
uniform float borderLower;
uniform float borderUpper;

out vec4 color;

#define POWDER_FLAG 64

#define TEXEL_PER 3

vec2 toSpacePos(vec2 pos) {
    return vec2(
        (pos.x - translate.x) / scale,
        -((pos.y - translate.y) / scale)
    );
}

void main() {
    vec2 currentPos = toSpacePos(vec2(gl_FragCoord.x, screenSize.y - gl_FragCoord.y));

    vec4 sum = vec4(0.0);
    float facSum = 0.0;
    float alphaSum = 0.0;

    float closestPowderDist = size + 1.0;
    vec4 closestPowderColor;
    for (int i=0; i<count; i++) {
        vec4 v1 = texelFetch(data, i * TEXEL_PER);

        vec2 pos = vec2(v1.xy);
        vec2 offset = abs(pos - currentPos);
        if (offset.x + offset.y > size * smoothUpper * 2.0) {
            continue;
        }

        vec4 v2 = texelFetch(data, i * TEXEL_PER + 1);
        vec4 v3 = texelFetch(data, i * TEXEL_PER + 2);

        vec4 col = vec4(v1.zw, v2.xy);
        vec2 vel = vec2(v2.zw);
        int flags = int(v3.x);

        float dist = distance(pos, currentPos);

        if ((flags & POWDER_FLAG) != 0) {
            if (dist < closestPowderDist) {
                closestPowderDist = dist;
                closestPowderColor = col;
            }
        } else {
            float factor = 1.0 - smoothstep(size * smoothLower, size * smoothUpper, dist);
            facSum += factor;
            sum += col * vec4(factor);
            alphaSum += col.a * factor;
        }
    }
    if (closestPowderDist > size) { // current pixel not in a powder particle
        color = sum;
        if (facSum > 1.0) {
            color /= facSum;
        } else {
            alphaSum /= facSum;
            color.a *= smoothstep(borderLower * alphaSum, borderUpper * alphaSum, color.a);
        }
    } else {
        color = closestPowderColor;
    }
}
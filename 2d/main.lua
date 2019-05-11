
local vec2 = require "vec2"
local shape = require "shape"

local cs = {}
local SHAPES = 10
local t = 0
local ray = {
	init = vec2(0, 0),
	dir = vec2(1, 1)
}

local function cast(ray, shapes)
	local dist = math.huge

	for i = 1, #shapes do
		dist = math.min(dist, shapes[i]:distanceTo(ray.init))
	end

	return { init = ray.init + ray.dir * dist, dir = ray.dir }, dist
end

function love.load()
	local w, h = love.window.getMode()

	math.randomseed(os.time())

	for i = 1, SHAPES do
		cs[#cs + 1] = shape.circle(vec2(math.random(100, w), math.random(100, h)), math.random(40, 100))
	end
end

function love.update(dt)
	t = t + dt
	local angle = (1 + math.sin(t)) * math.pi / 4
	local dx = math.sin(angle)
	local dy = math.cos(angle)
	ray.dir = vec2(dx, dy)
end

function love.draw()
	local m = vec2(love.mouse.getPosition())
	local r, d = ray, 1
	local c = 0

	for i = 1, #cs do
		love.graphics.circle("line", cs[i].position.x, cs[i].position.y, cs[i].radius)
	end

	while c < 20 and math.abs(d) > 0.1 do
		local rr = r
		r, d = cast(r, cs)
		love.graphics.circle("line", rr.init.x, rr.init.y, d)
		love.graphics.line(rr.init.x, rr.init.y, r.init.x, r.init.y)
		c = c + 1
	end
end

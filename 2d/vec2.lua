
local vec2 = {}
vec2.__index = vec2

function vec2:__call(x, y)
	return setmetatable({x = x, y = y}, self)
end

function vec2:length()
	return math.sqrt(self.x * self.x + self.y * self.y)
end

function vec2:__add(other)
	return vec2(self.x + other.x, self.y + other.y)
end

function vec2:__sub(other)
	return vec2(self.x - other.x, self.y - other.y)
end

function vec2:__mul(other)
	return vec2(self.x * other, self.y * other)
end

function vec2:__tostring()
	return "(" .. self.x .. ", " .. self.y .. ")"
end

return setmetatable(vec2, vec2)

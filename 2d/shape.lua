
local shape = {}

function shape.circle(position, radius)
	return {
		position = position,
		radius = radius,
		distanceTo = function(self, point)
			return (point - self.position):length() - self.radius
		end
	}
end

return shape

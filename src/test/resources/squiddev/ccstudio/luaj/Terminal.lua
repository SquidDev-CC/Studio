-- Something was breaking with the terminal object

-- Create tiny term object
local native = (a.native and a.native()) or a
local redirectTarget = native
local redirect = function(target)
	for k, v in pairs(native) do
		target[k] = function()
			error("Redirect object is missing method " .. k .. ".", 2)
		end
	end
	local oldRedirectTarget = redirectTarget
	redirectTarget = target
	return oldRedirectTarget
end

while false do end

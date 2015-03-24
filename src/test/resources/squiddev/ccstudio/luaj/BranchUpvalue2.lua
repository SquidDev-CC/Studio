local a

local function render()
	return a
end

while true do
	if 8 == 8 then
		a = 8
	else
		for i = 1, 8 do
			for k, v in ipairs(i) do
				error(i .. k .. v)
			end
		end
	end

	error("error")
end

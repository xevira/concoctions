package com.xevira.concoctions.client.gui.widgets;

import javax.annotation.Nullable;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.button.Button.IPressable;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.fml.client.gui.GuiUtils;
import net.minecraftforge.fml.client.gui.widget.Slider;
import net.minecraftforge.fml.client.gui.widget.Slider.ISlider;

public class SliderVertical extends Slider
{
	public double sliderStep = 0.1D;
	
    public SliderVertical(int xPos, int yPos, int width, int height, ITextComponent prefix, ITextComponent suf, double minVal, double maxVal, double stepVal, double currentVal, boolean showDec, boolean drawStr, IPressable handler)
    {
    	super(xPos, yPos, width, height, prefix, suf, minVal, maxVal, currentVal, showDec, drawStr, handler);
    	this.sliderStep = stepVal;
    }

    public SliderVertical(int xPos, int yPos, int width, int height, ITextComponent prefix, ITextComponent suf, double minVal, double maxVal, double stepVal, double currentVal, boolean showDec, boolean drawStr, IPressable handler, @Nullable ISlider par)
    {
    	super(xPos, yPos, width, height, prefix, suf, minVal, maxVal, currentVal, showDec, drawStr, handler, par);
    	this.sliderStep = stepVal;
    }

    public SliderVertical(int xPos, int yPos, ITextComponent displayStr, double minVal, double maxVal, double stepVal, double currentVal, IPressable handler, ISlider par)
    {
    	super(xPos, yPos, displayStr, minVal, maxVal, currentVal, handler, par);
    	this.sliderStep = stepVal;
    }

    @Override
    public int getYImage(boolean par1)
    {
        return 0;
    }
    
    /**
     * Fired when the mouse button is dragged. Equivalent of MouseListener.mouseDragged(MouseEvent e).
     */
    @Override
    protected void renderBg(MatrixStack mStack, Minecraft par1Minecraft, int par2, int par3)
    {
        if (this.visible)
        {
            if (this.dragging)
            {
                this.sliderValue = (par3 - (this.y + 4)) / (float)(this.height - 8);
                updateSlider();
            }

            GuiUtils.drawContinuousTexturedBox(mStack, WIDGETS_LOCATION, this.x, this.y + (int)(this.sliderValue * (float)(this.height - 8)), 0, 66, this.width, 8, 200, 20, 2, 3, 2, 2, this.getBlitOffset());
        }
    }
    
    @Override
    public void onClick(double mouseX, double mouseY)
    {
        this.sliderValue = (mouseY - (this.y + 4)) / (this.height - 8);
        updateSlider();
        this.dragging = true;
    }
    
    public void updateSlider()
    {
        if (this.sliderStep > 0.0F && maxValue > minValue)
        {
        	double pos = sliderValue * (maxValue - minValue) + minValue;
        	int steps = (int)Math.round(pos / this.sliderStep);
        	
        	pos = steps * this.sliderStep + minValue;
        	this.sliderValue = (pos - minValue) / (maxValue - minValue);
        }

        if (this.sliderValue < 0.0F)
        {
            this.sliderValue = 0.0F;
        }

        if (this.sliderValue > 1.0F)
        {
            this.sliderValue = 1.0F;
        }
        
        String val;

        if (showDecimal)
        {
            val = Double.toString(sliderValue * (maxValue - minValue) + minValue);

            if (val.substring(val.indexOf(".") + 1).length() > precision)
            {
                val = val.substring(0, val.indexOf(".") + precision + 1);

                if (val.endsWith("."))
                {
                    val = val.substring(0, val.indexOf(".") + precision);
                }
            }
            else
            {
                while (val.substring(val.indexOf(".") + 1).length() < precision)
                {
                    val = val + "0";
                }
            }
        }
        else
        {
            val = Integer.toString((int)Math.round(sliderValue * (maxValue - minValue) + minValue));
        }

        if(drawString)
        {
            setMessage(new StringTextComponent("").append(dispString).appendString(val).append(suffix));
        }

        if (parent != null)
        {
            parent.onChangeSliderValue(this);
        }
    }

    /**
     * Fired when the mouse button is released. Equivalent of MouseListener.mouseReleased(MouseEvent e).
     */
    @Override
    public void onRelease(double mouseX, double mouseY)
    {
        this.dragging = false;
    }

    public int getValueInt()
    {
        return (int)Math.round(sliderValue * (maxValue - minValue) + minValue);
    }

    public double getValue()
    {
        return sliderValue * (maxValue - minValue) + minValue;
    }

    public void setValue(double d)
    {
        this.sliderValue = (d - minValue) / (maxValue - minValue);
    }
}
